export interface NotificationTemplate {
  id: number;
  template_code: string;
  title: string;
  description?: string;
  message_content: string;
  template_type: TemplateType;
  category: string;
  is_active: boolean;
  version: number;
  created_by: string;
  updated_by?: string;
  created_at: string;
  updated_at: string;
}

export enum TemplateType {
  SMS = 'SMS',
  EMAIL = 'EMAIL',
  PUSH = 'PUSH',
  WHATSAPP = 'WHATSAPP'
}

export enum VariableType {
  STRING = 'STRING',
  NUMBER = 'NUMBER',
  DATE = 'DATE',
  BOOLEAN = 'BOOLEAN'
}

export interface TemplateVariable {
  id: number;
  template_id: number;
  variable_name: string;
  variable_type: VariableType;
  default_value?: string;
  is_required: boolean;
  description?: string;
  created_at: string;
}

export interface TemplateVersion {
  id: number;
  template_id: number;
  version: number;
  message_content: string;
  variables_snapshot?: any;
  created_by: string;
  created_at: string;
  is_active: boolean;
}

export interface CreateTemplateRequest {
  template_code: string;
  title: string;
  description?: string;
  message_content: string;
  template_type: TemplateType;
  category: string;
  variables?: CreateTemplateVariable[];
}

export interface UpdateTemplateRequest {
  title?: string;
  description?: string;
  message_content?: string;
  template_type?: TemplateType;
  category?: string;
  is_active?: boolean;
  variables?: CreateTemplateVariable[];
}

export interface CreateTemplateVariable {
  variable_name: string;
  variable_type: VariableType;
  default_value?: string;
  is_required: boolean;
  description?: string;
}

export interface TemplateWithVariables extends NotificationTemplate {
  variables: TemplateVariable[];
}

export interface ProcessMessageRequest {
  template_code: string;
  variables: Record<string, any>;
}

export interface ProcessMessageResponse {
  processed_message: string;
  template_code: string;
  variables_used: Record<string, any>;
}

export interface SendNotificationRequest {
  template_code: string;
  variables: Record<string, any>;
  recipient: string;
  template_type?: TemplateType;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  search?: string;
  category?: string;
  template_type?: TemplateType;
  is_active?: boolean;
}