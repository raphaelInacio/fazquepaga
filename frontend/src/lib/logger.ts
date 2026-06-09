import api from '@/lib/api';

type LogLevel = 'error' | 'warn' | 'info';

interface LogPayload {
  message: string;
  stack?: string;
  component: string;
  requestUri: string;
  metadata?: Record<string, unknown>;
}

function buildRequestUri(): string {
  if (typeof window === 'undefined') return '/';
  return window.location.pathname + window.location.search;
}

function buildBaseMetadata(): Record<string, unknown> {
  if (typeof window === 'undefined') return {};
  return {
    userAgent: navigator.userAgent,
    language: navigator.language,
    timestamp: new Date().toISOString(),
    url: window.location.href,
  };
}

async function sendLog(level: LogLevel, payload: LogPayload): Promise<void> {
  if (level !== 'error') return;

  try {
    await api.post('/api/v1/logs/client', {
      message: payload.message,
      stack: payload.stack ?? '',
      component: payload.component,
      requestUri: payload.requestUri,
      metadata: {
        ...buildBaseMetadata(),
        ...payload.metadata,
        level,
      },
    });
  } catch {
    // Falha silenciosa — não propagar erro de logging
  }
}

export const logger = {
  error(message: string, component: string, error?: unknown, metadata?: Record<string, unknown>) {
    const stack = error instanceof Error ? error.stack : undefined;
    const requestUri = buildRequestUri();
    void sendLog('error', { message, stack, component, requestUri, metadata });
  },
};
