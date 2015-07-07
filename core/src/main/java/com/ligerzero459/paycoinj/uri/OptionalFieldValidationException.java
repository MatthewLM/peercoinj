package com.ligerzero459.paycoinj.uri;

/**
 * <p>Exception to provide the following to {@link org.multibit.qrcode.PeercoinURI}:</p>
 * <ul>
 * <li>Provision of parsing error messages</li>
 * </ul>
 * <p>This exception occurs when an optional field is detected (under the Peercoin URI scheme) and fails
 * to pass the associated test (such as {@code amount} not being a valid number).</p>
 *
 * @since 0.3.0
 *         
 */
public class OptionalFieldValidationException extends PeercoinURIParseException {

    public OptionalFieldValidationException(String s) {
        super(s);
    }

    public OptionalFieldValidationException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
