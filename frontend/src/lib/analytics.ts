declare global {
  interface Window {
    dataLayer: any[];
  }
}

/**
 * Pushes an event to the Google Tag Manager dataLayer.
 * @param eventName The name of the event (e.g., 'generate_lead', 'begin_checkout')
 * @param eventParams Optional parameters associated with the event
 */
export const trackEvent = (eventName: string, eventParams?: Record<string, any>) => {
  if (typeof window !== 'undefined') {
    window.dataLayer = window.dataLayer || [];
    window.dataLayer.push({
      event: eventName,
      ...eventParams,
    });
  }
};
