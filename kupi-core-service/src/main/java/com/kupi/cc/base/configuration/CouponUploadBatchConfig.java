package com.kupi.cc.base.configuration;

import com.kupi.cc.base.domain.entity.Coupon;
import com.kupi.cc.base.domain.entity.CouponCsv;
import com.kupi.cc.base.enums.UserType;
import com.kupi.cc.base.service.CouponCodeGeneratorService;
import com.kupi.cc.base.service.DealService;
import com.kupi.cc.base.util.DateParserUtil;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Spring Batch Configuration for Java 21 + Spring Boot 3.2.6
 * Updated from Spring Batch 4.x to 5.x patterns:
 * - Removed @EnableBatchProcessing (auto-configured by Spring Boot 3.2.6)
 * - Replaced JobBuilderFactory with JobBuilder (injected as parameter)
 * - Replaced StepBuilderFactory with StepBuilder (injected as parameter)
 */
@Configuration
public class CouponUploadBatchConfig {

    private static final String CSV_READER = "csvReader";
    private static final String CSV_PROPERTY_EXPIRY_DATE = "expiryDate";
    private static final String CSV_PROPERTY_COUPON_COUNT = "couponCount";
    private static final String JOB_NAME = "importCoupons";
    private static final String SETUP_NAME = "csv-step";
    private static final String DELIMITER = ",";
    private static final String FILE_NAME = "FILE_NAME";
    private static final String TIME_ZONE = "TIME_ZONE";
    private static final String DEAL_ID = "DEAL_ID";

    @Autowired
    private DealService dealService;

    @Autowired
    private CouponCodeGeneratorService couponCodeGeneratorService;

    @Autowired
    private CouponListWriter couponListWriter;

    @Autowired
    private DateParserUtil dateParserUtil;

    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public FlatFileItemReader<CouponCsv> reader(@Value("#{jobParameters['fileName']}") String fileName,
                                                @Value("${couponsUpload.csv.saveLocation}") String saveLocation) {
        FlatFileItemReader<CouponCsv> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource(saveLocation + fileName));
        itemReader.setName(CSV_READER);
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    /**
     * This method creates a line mapper for mapping line (string) to domain object.
     * For automated coupon generation (expiryDate, couponCount)
     *
     * @return LineMapper
     */
    private LineMapper<CouponCsv> lineMapper() {
        DefaultLineMapper<CouponCsv> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(DELIMITER);
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames(CSV_PROPERTY_EXPIRY_DATE, CSV_PROPERTY_COUPON_COUNT);

        BeanWrapperFieldSetMapper<CouponCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(CouponCsv.class);
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

    /**
     * This method used processes the data read in to the spring batch.
     *
     * @param dealId   dealId
     * @param fileName fileName
     * @return CouponUploadBatchProcessor
     */
    @Bean
    @Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public CouponUploadBatchProcessor processors(@Value("#{jobParameters['dealId']}") String dealId,
                                                 @Value("#{jobParameters['fileName']}") String fileName,
                                                 @Value("#{jobParameters['timeZone']}") String timeZone) {
        var deal = dealService.getDeal(dealId, UserType.MERCHANT);
        return new CouponUploadBatchProcessor(deal, fileName, timeZone, couponCodeGeneratorService, dateParserUtil);
    }


    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        StepBuilder stepBuilder = new StepBuilder(SETUP_NAME, jobRepository);
        return stepBuilder
            .<CouponCsv, List<Coupon>>chunk(100, transactionManager)
            .reader(reader(FILE_NAME, ""))
            .processor(processors(DEAL_ID, FILE_NAME, TIME_ZONE))
            .writer(couponListWriter)
            .taskExecutor(taskExecutor())
            .build();
    }

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        JobBuilder jobBuilder = new JobBuilder(JOB_NAME, jobRepository);
        return jobBuilder
            .incrementer(new RunIdIncrementer())
            .flow(step(jobRepository, transactionManager))
            .end()
            .build();
    }

    /**
     * This method returns a TaskExecutor.
     *
     * @return TaskExecutor
     */
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(50);
        return simpleAsyncTaskExecutor;
    }
}
