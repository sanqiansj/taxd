package com.niaobulashi.Utils;

import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/** status enum */
public enum Status {
  SUCCESS(200, "success", "success"),
  SUCCESS_MSG(201, "success", "成功{0}"),
  INTERNAL_SERVER_ERROR_ARGS(10000, "Internal Server Error: {0}", "服务端异常: {0}"),
  QUERY_RESULT_IS_NULL(10001, "", "未查询到{0}数据"),
  REQUEST_PARAMS_NOT_VALID_ERROR(10002, "request parameter {0} is not valid", "请求参数[{0}]无效"),

  USER_NAME_EXIST(10003, "user name already exists", "用户名已存在"),
  USER_NAME_NULL(10004, "user name is null", "用户名不能为空"),
  USER_NOT_EXIST(10010, "user {0} not exists", "用户[{0}]不存在"),
  USER_NAME_PASSWD_ERROR(10013, "user name or password error", "用户名或密码错误"),
  LOGIN_SESSION_FAILED(10014, "create session failed!", "创建session失败"),
  DATASOURCE_EXIST(10015, "data source name already exists", "数据源名称已存在"),
  DATASOURCE_CONNECT_FAILED(10016, "data source connection failed", "建立数据源连接失败"),
  RULE_CONFIG_ALREADY_EXISTS(
      10019, "rule config {0} already exists", "配置[{0}]已存在,请检查rule id /rule name 是否重复"),
  CREATE_RULE_CONFIG_ERROR(10048, "create rule config  error", "创建规则配置错误"),
  UPDATE_RULE_CONFIG_ERROR(10046, "update rule config error", "更新规则配置错误"),
  RULE_CONFIG_NOT_EXIST(10004, "rule config  not exist", "配置不存在"),
  CREATE_ACCOUNT_CONFIG_ERROR(20048, "create account  config  error", "创建账户配置错误"),
  UPDATE_ACCOUNT_CONFIG_ERROR(20046, "update account  config error", "更新账户配置错误"),
  ACCOUNT_CONFIG_NOT_EXIST(20004, "account  config not exist", "账户配置不存在"),
  LOGIN_SUCCESS(10042, "login success", "登录成功"),
  USER_LOGIN_FAILURE(10043, "user login failure", "用户登录失败"),

  KEYWORD_CONFIG_ALREADY_EXISTI(30019, "keyword config {0} already exists", "配置[{0}]已存在"),
  CREATE_KEYWORD_CONFIG_ERROR(30048, "create keyword config  error", "创建关键词配置错误"),
  UPDATE_KEYWORD_CONFIG_ERROR(30046, "update keyword config error", "更新关键词配置错误"),
  KEYWORD_CONFIG_NOT_EXIST(30004, "keyword config  not exist", "关键词配置不存在"),

  HBASE_SEARCH_EXCEPTION(40004, "rule config  not exist", "查询hbase失败，错误原因{0}"),
  ;

  private final int code;
  private final String enMsg;
  private final String zhMsg;

  Status(int code, String enMsg, String zhMsg) {
    this.code = code;
    this.enMsg = enMsg;
    this.zhMsg = zhMsg;
  }

  public int getCode() {
    return this.code;
  }

  public String getMsg() {
    if (Locale.SIMPLIFIED_CHINESE
        .getLanguage()
        .equals(LocaleContextHolder.getLocale().getLanguage())) {
      return this.zhMsg;
    } else {
      return this.enMsg;
    }
  }
}
