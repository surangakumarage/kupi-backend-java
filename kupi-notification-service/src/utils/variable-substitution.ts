import { TemplateVariable, VariableType } from '../types/template';
import { ValidationError } from './validation';

export interface SubstitutionResult {
  processedMessage: string;
  variablesUsed: Record<string, any>;
  missingVariables: string[];
}

/**
 * Substitutes variables in a message template with actual values
 * Variables in the message are prefixed with $ (e.g., $userName, $orderId)
 */
export const substituteVariables = (
  message: string,
  variables: Record<string, any>,
  templateVariables: TemplateVariable[] = []
): SubstitutionResult => {
  let processedMessage = message;
  const variablesUsed: Record<string, any> = {};
  const missingVariables: string[] = [];

  // Create a map of template variables for validation
  const templateVarMap = new Map(
    templateVariables.map(tv => [tv.variable_name, tv])
  );

  // Extract all variables from the message (those with $ prefix)
  const variablePattern = /\$([a-zA-Z][a-zA-Z0-9_]*)/g;
  const messageVariables = [...new Set([...message.matchAll(variablePattern)].map(match => match[1]))];

  // Process each variable found in the message
  for (const variableName of messageVariables) {
    const templateVar = templateVarMap.get(variableName);
    let value: any;

    // Check if variable is provided in the variables object
    if (variableName in variables) {
      value = variables[variableName];

      // Validate and format the value if template variable definition exists
      if (templateVar) {
        try {
          value = validateAndFormatVariable(value, templateVar);
        } catch (error) {
          throw new ValidationError(`Invalid value for variable '${variableName}': ${error.message}`);
        }
      }

      variablesUsed[variableName] = value;
    } else {
      // Check if template variable has a default value
      if (templateVar?.default_value !== null && templateVar?.default_value !== undefined) {
        value = validateAndFormatVariable(templateVar.default_value, templateVar);
        variablesUsed[variableName] = value;
      } else {
        // Variable is missing and no default value
        missingVariables.push(variableName);

        // If it's a required variable, we'll throw an error later
        if (templateVar?.is_required) {
          throw new ValidationError(`Required variable '${variableName}' is missing and has no default value`);
        }

        // For non-required variables without defaults, leave the placeholder
        continue;
      }
    }

    // Replace all occurrences of this variable in the message
    const variableRegex = new RegExp(`\\$${variableName}`, 'g');
    processedMessage = processedMessage.replace(variableRegex, String(value));
  }

  return {
    processedMessage,
    variablesUsed,
    missingVariables
  };
};

/**
 * Validates and formats a variable value according to its type definition
 */
export const validateAndFormatVariable = (value: any, templateVariable: TemplateVariable): any => {
  const { variable_type, variable_name } = templateVariable;

  // Handle null/undefined values
  if (value === null || value === undefined) {
    if (templateVariable.is_required) {
      throw new ValidationError(`Required variable '${variable_name}' cannot be null or undefined`);
    }
    return templateVariable.default_value || '';
  }

  switch (variable_type) {
    case VariableType.STRING:
      return String(value);

    case VariableType.NUMBER:
      const numValue = Number(value);
      if (isNaN(numValue)) {
        throw new ValidationError(`Variable '${variable_name}' must be a valid number, received: ${value}`);
      }
      return numValue;

    case VariableType.DATE:
      if (typeof value === 'string') {
        const date = new Date(value);
        if (isNaN(date.getTime())) {
          throw new ValidationError(`Variable '${variable_name}' must be a valid date string, received: ${value}`);
        }
        return formatDate(date);
      } else if (value instanceof Date) {
        if (isNaN(value.getTime())) {
          throw new ValidationError(`Variable '${variable_name}' must be a valid Date object`);
        }
        return formatDate(value);
      } else {
        throw new ValidationError(`Variable '${variable_name}' must be a Date object or valid date string, received: ${typeof value}`);
      }

    case VariableType.BOOLEAN:
      if (typeof value === 'boolean') {
        return value ? 'true' : 'false';
      } else if (typeof value === 'string') {
        const lowerValue = value.toLowerCase();
        if (lowerValue === 'true' || lowerValue === '1' || lowerValue === 'yes') {
          return 'true';
        } else if (lowerValue === 'false' || lowerValue === '0' || lowerValue === 'no') {
          return 'false';
        } else {
          throw new ValidationError(`Variable '${variable_name}' must be a valid boolean value, received: ${value}`);
        }
      } else {
        throw new ValidationError(`Variable '${variable_name}' must be a boolean or boolean string, received: ${typeof value}`);
      }

    default:
      throw new ValidationError(`Unknown variable type: ${variable_type}`);
  }
};

/**
 * Formats a date according to application standards
 */
export const formatDate = (date: Date): string => {
  // Format as YYYY-MM-DD HH:mm:ss in local timezone
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

/**
 * Extracts all variable names from a message template
 */
export const extractVariableNames = (message: string): string[] => {
  const variablePattern = /\$([a-zA-Z][a-zA-Z0-9_]*)/g;
  const matches = [...message.matchAll(variablePattern)];
  return [...new Set(matches.map(match => match[1]))];
};

/**
 * Validates that all required variables are provided
 */
export const validateRequiredVariables = (
  variables: Record<string, any>,
  templateVariables: TemplateVariable[]
): void => {
  const requiredVariables = templateVariables
    .filter(tv => tv.is_required)
    .map(tv => tv.variable_name);

  const missingRequired = requiredVariables.filter(variable =>
    !(variable in variables) || variables[variable] === null || variables[variable] === undefined
  );

  if (missingRequired.length > 0) {
    throw new ValidationError(`Missing required variables: ${missingRequired.join(', ')}`);
  }
};

/**
 * Preview message substitution without throwing errors for missing variables
 */
export const previewMessageSubstitution = (
  message: string,
  variables: Record<string, any>,
  templateVariables: TemplateVariable[] = []
): SubstitutionResult => {
  try {
    return substituteVariables(message, variables, templateVariables);
  } catch (error) {
    // For preview, return partial substitution even if some variables are missing
    const messageVariables = extractVariableNames(message);
    let processedMessage = message;
    const variablesUsed: Record<string, any> = {};
    const missingVariables: string[] = [];

    for (const variableName of messageVariables) {
      if (variableName in variables && variables[variableName] !== null && variables[variableName] !== undefined) {
        const variableRegex = new RegExp(`\\$${variableName}`, 'g');
        processedMessage = processedMessage.replace(variableRegex, String(variables[variableName]));
        variablesUsed[variableName] = variables[variableName];
      } else {
        missingVariables.push(variableName);
      }
    }

    return {
      processedMessage,
      variablesUsed,
      missingVariables
    };
  }
};