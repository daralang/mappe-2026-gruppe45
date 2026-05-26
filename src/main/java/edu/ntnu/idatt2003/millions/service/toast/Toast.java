// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service.toast;

/**
 * A brief user-facing notification displayed as a transient overlay.
 *
 * @param id      unique identifier for this toast
 * @param type    severity category of the notification
 * @param message text to display to the user
 */
public record Toast(long id, ToastType type, String message) {}
