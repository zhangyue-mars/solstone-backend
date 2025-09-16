ALTER TABLE chat_model 
ADD COLUMN use_system_prompt TINYINT(1) DEFAULT 1 COMMENT '是否使用系统提示词（0否 1是）';