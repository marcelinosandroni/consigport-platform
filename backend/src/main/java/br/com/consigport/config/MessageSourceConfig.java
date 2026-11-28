package br.com.consigport.config;

import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * The bundles of the human messages and the negotiation of the locale.
 *
 * <p>The error code is the contract and never changes; the message is the presentation
 * and follows the {@code Accept-Language} of the caller. The negotiation accepts
 * {@code pt-BR}, {@code en} and {@code es}, and falls back to {@code pt-BR} when the
 * caller asks for a language the API does not speak.
 *
 * <p>The validator reads the same bundles, so the detail of a validation failure is
 * localized with the message of the error instead of with the default of the JVM.
 */
@Configuration
public class MessageSourceConfig {

  /** The default locale of the API. */
  public static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("pt-BR");

  /** The locales the API speaks. */
  public static final List<Locale> SUPPORTED_LOCALES =
      List.of(DEFAULT_LOCALE, Locale.ENGLISH, Locale.forLanguageTag("es"));

  /** The basename of the bundles of the messages. */
  public static final String BUNDLE_BASENAME = "i18n/messages";

  /**
   * The source of the messages of the API.
   *
   * @return the source that reads the bundles
   */
  @Bean
  public MessageSource messageSource() {
    return createMessageSource();
  }

  /**
   * The resolver that reads the locale of the caller.
   *
   * @return the resolver of the locale
   */
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(DEFAULT_LOCALE);
    resolver.setSupportedLocales(SUPPORTED_LOCALES);
    return resolver;
  }

  /**
   * The validator of the request, with the messages of the bundles.
   *
   * @param messageSource the source of the messages
   * @return the validator that interpolates the messages in the locale of the caller
   */
  @Bean
  public LocalValidatorFactoryBean validator(MessageSource messageSource) {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.setValidationMessageSource(messageSource);
    return validator;
  }

  /**
   * Builds the source of the bundles.
   *
   * <p>The method is public so the tests of the edge read the very same bundles the
   * application reads, instead of a copy of the configuration.
   *
   * @return the source that reads the bundles
   */
  public static MessageSource createMessageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename(BUNDLE_BASENAME);
    source.setDefaultEncoding("UTF-8");
    source.setFallbackToSystemLocale(false);
    source.setUseCodeAsDefaultMessage(false);
    return source;
  }
}
