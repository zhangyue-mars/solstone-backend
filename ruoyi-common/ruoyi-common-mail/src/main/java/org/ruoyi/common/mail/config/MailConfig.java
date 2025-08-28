package org.ruoyi.common.mail.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.ruoyi.common.core.service.ConfigService;
import org.ruoyi.common.mail.utils.MailAccount;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * JavaMail 配置
 *
 * @author Michelle.Chung
 */

@RequiredArgsConstructor
@Configuration
@Slf4j
public class MailConfig {

    private final ConfigService configService;
    private MailAccount account;

    @Bean
    public MailAccount mailAccount() {
        account = new MailAccount();
        updateMailAccount();
        return account;
    }

//    public void updateMailAccount() {
//        account.setHost(getKey("host"));
//        account.setPort(NumberUtils.toInt(getKey("port"), 465));
//        account.setAuth(true);
//        account.setFrom(getKey("from"));
//        account.setUser(getKey("user"));
//        account.setPass(getKey("pass"));
//        account.setSocketFactoryPort(NumberUtils.toInt(getKey("port"), 465));
//        account.setStarttlsEnable(true);
//        account.setSslEnable(true);
//        account.setTimeout(0);
//        account.setConnectionTimeout(0);
//    }
    public void updateMailAccount() {
        account.setHost(getKey("host")); // SMTP 主机
        account.setPort(NumberUtils.toInt(getKey("port"), 465)); // SSL 端口
        account.setAuth(true);
        account.setFrom(getKey("from"));
        account.setUser(getKey("user")); // 邮箱账号
        account.setPass(getKey("pass")); // 授权码

        // SSL 直连，不用 STARTTLS
        account.setSslEnable(true);
        account.setStarttlsEnable(false);

        // 建议去掉 SocketFactory 配置，让 JavaMail 默认处理
        // account.setSocketFactoryClass("javax.net.ssl.SSLSocketFactory");
        // account.setSocketFactoryPort(account.getPort());

        account.setTimeout(30000);           // 可设置超时 30 秒
        account.setConnectionTimeout(30000); // 连接超时 30 秒
    }


    public String getKey(String key){
        return configService.getConfigValue("mail", key);
    }
}
