import { CreateTemplateRequest, UpdateTemplateRequest, TemplateType, VariableType } from '../types/template';

export class ValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'ValidationError';
  }
}

export const validateTemplateCode = (templateCode: string): void => {
  if (!templateCode || typeof templateCode !== 'string') {
    throw new ValidationError('Template code is required');
  }

  // Template code should be uppercase with underscores
  const templateCodePattern = /^[A-Z][A-Z0-9_]*[A-Z0-9]$/;
  if (!templateCodePattern.test(templateCode)) {
    throw new ValidationError('Template code must be uppercase letters, numbers, and underscores only (e.g., WELCOME_USER_SMS)');
  }

  if (templateCode.length < 3 || templateCode.length > 50) {
    throw new ValidationError('Template code must be between 3 and 50 characters');
  }
};

export const validateCreateTemplate = (data: CreateTemplateRequest): void => {
  // Validate required fields
  if (!data.template_code) {
    throw new ValidationError('Template code is required');
  }

  if (!data.title || data.title.trim().length === 0) {
    throw new ValidationError('Title is required');
  }

  if (!data.message_content || data.message_content.trim().length === 0) {
    throw new ValidationError('Message content is required');
  }

  if (!data.template_type) {
    throw new ValidationError('Template type is required');
  }

  if (!data.category || data.category.trim().length === 0) {
    throw new ValidationError('Category is required');
  }

  // Validate template code format
  validateTemplateCode(data.template_code);

  // Validate title length
  if (data.title.length > 255) {
    throw new ValidationError('Title cannot exceed 255 characters');
  }

  // Validate template type
  if (!Object.values(TemplateType).includes(data.template_type)) {
    throw new ValidationError(`Invalid template type. Must be one of: ${Object.values(TemplateType).join(', ')}`);
  }

  // Validate category length
  if (data.category.length > 100) {
    throw new ValidationError('Category cannot exceed 100 characters');
  }

  // Validate message content contains at least one variable if variables are provided
  if (data.variables && data.variables.length > 0) {
    const messageVariables = extractVariablesFromMessage(data.message_content);
    const providedVariables = data.variables.map(v => v.variable_name);

    // Check if all required variables are present in message
    const requiredVariables = data.variables.filter(v => v.is_required).map(v => v.variable_name);
    const missingRequired = requiredVariables.filter(v => !messageVariables.includes(v));

    if (missingRequired.length > 0) {
      throw new ValidationError(`Required variables not found in message content: ${missingRequired.join(', ')}`);
    }

    // Check for undefined variables in message
    const undefinedVariables = messageVariables.filter(v => !providedVariables.includes(v));

    if (undefinedVariables.length > 0) {
      console.warn(`Warning: Variables found in message but not defined: ${undefinedVariables.join(', ')}`);
    }
  }

  // Validate variables if provided
  if (data.variables) {
    data.variables.forEach((variable, index) => {
      validateTemplateVariable(variable, index);
    });
  }
};

export const validateUpdateTemplate = (data: UpdateTemplateRequest): void => {
  // At least one field should be provided for update
  const hasUpdate = data.title || data.description !== undefined || data.message_content ||
                   data.template_type || data.category || data.is_active !== undefined || data.variables;

  if (!hasUpdate) {
    throw new ValidationError('At least one field must be provided for update');
  }

  // Validate individual fields if provided
  if (data.title !== undefined) {
    if (typeof data.title !== 'string' || data.title.trim().length === 0) {
      throw new ValidationError('Title cannot be empty');
    }
    if (data.title.length > 255) {
      throw new ValidationError('Title cannot exceed 255 characters');
    }
  }

  if (data.message_content !== undefined) {
    if (typeof data.message_content !== 'string' || data.message_content.trim().length === 0) {
      throw new ValidationError('Message content cannot be empty');
    }
  }

  if (data.template_type !== undefined) {
    if (!Object.values(TemplateType).includes(data.template_type)) {
      throw new ValidationError(`Invalid template type. Must be one of: ${Object.values(TemplateType).join(', ')}`);
    }
  }

  if (data.category !== undefined) {
    if (typeof data.category !== 'string' || data.category.trim().length === 0) {
      throw new ValidationError('Category cannot be empty');
    }
    if (data.category.length > 100) {
      throw new ValidationError('Category cannot exceed 100 characters');
    }
  }

  if (data.is_active !== undefined) {
    if (typeof data.is_active !== 'boolean') {
      throw new ValidationError('is_active must be a boolean value');
    }
  }

  // Validate variables if provided
  if (data.variables) {
    data.variables.forEach((variable, index) => {
      validateTemplateVariable(variable, index);
    });
  }
};

export const validateTemplateVariable = (variable: any, index?: number): void => {
  const prefix = index !== undefined ? `Variable ${index + 1}:` : 'Variable:';

  if (!variable.variable_name || typeof variable.variable_name !== 'string') {
    throw new ValidationError(`${prefix} Variable name is required`);
  }

  // Variable name should be camelCase or snake_case
  const variableNamePattern = /^[a-zA-Z][a-zA-Z0-9_]*$/;
  if (!variableNamePattern.test(variable.variable_name)) {
    throw new ValidationError(`${prefix} Variable name must start with a letter and contain only letters, numbers, and underscores`);
  }

  if (variable.variable_name.length > 100) {
    throw new ValidationError(`${prefix} Variable name cannot exceed 100 characters`);
  }

  if (!variable.variable_type) {
    throw new ValidationError(`${prefix} Variable type is required`);
  }

  if (!Object.values(VariableType).includes(variable.variable_type)) {
    throw new ValidationError(`${prefix} Invalid variable type. Must be one of: ${Object.values(VariableType).join(', ')}`);
  }

  if (variable.is_required !== undefined && typeof variable.is_required !== 'boolean') {
    throw new ValidationError(`${prefix} is_required must be a boolean value`);
  }
};

export const extractVariablesFromMessage = (message: string): string[] => {
  // Extract variables with $ prefix (e.g., $userName, $orderId)
  const variablePattern = /\$([a-zA-Z][a-zA-Z0-9_]*)/g;
  const matches = message.match(variablePattern);

  if (!matches) {
    return [];
  }

  // Remove $ prefix and return unique variable names
  return [...new Set(matches.map(match => match.substring(1)))];
};

export const validateVariableSubstitution = (
  message: string,
  variables: Record<string, any>,
  requiredVariables: string[]
): void => {
  // Check if all required variables are provided
  const missingRequired = requiredVariables.filter(variable => !(variable in variables));

  if (missingRequired.length > 0) {
    throw new ValidationError(`Missing required variables: ${missingRequired.join(', ')}`);
  }

  // Extract variables from message
  const messageVariables = extractVariablesFromMessage(message);

  // Check for undefined variables in the provided variables object
  const undefinedVariables = messageVariables.filter(variable => !(variable in variables));

  if (undefinedVariables.length > 0) {
    throw new ValidationError(`Variables used in message but not provided: ${undefinedVariables.join(', ')}`);
  }
};

export const validatePaginationParams = (params: any): { page: number; size: number } => {
  let page = 1;
  let size = 10;

  if (params.page) {
    page = parseInt(params.page);
    if (isNaN(page) || page < 1) {
      throw new ValidationError('Page must be a positive integer');
    }
  }

  if (params.size) {
    size = parseInt(params.size);
    if (isNaN(size) || size < 1 || size > 100) {
      throw new ValidationError('Size must be a positive integer between 1 and 100');
    }
  }

  return { page, size };
};