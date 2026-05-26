package edu.ntnu.idatt2003.millions.file;

/**
 * Implemented by exceptions that carry an i18n key and format arguments instead
 * of a pre-resolved error message. Callers can catch the interface type and
 * resolve the message at display time via
 * {@link java.text.MessageFormat#format(String, Object...)} against the user's
 * current language bundle.
 *
 * <p>This separation lets the file and domain layers stay language-agnostic —
 * they raise structured exceptions and leave string resolution to the
 * presentation layer.</p>
 */
public interface LocalizedException {

    /**
     * Returns the resource-bundle key for the error message template.
     *
     * @return the i18n key; never null
     */
    String getI18nKey();

    /**
     * Returns the format arguments for the i18n message template.
     *
     * @return the format arguments; never null, may be empty
     */
    Object[] getArgs();
}
