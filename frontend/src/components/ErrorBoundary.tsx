import React, { Component, ErrorInfo, ReactNode } from 'react';
import api from '@/lib/api';
import { AlertTriangle, RefreshCw, Home } from 'lucide-react';
import { Button } from './ui/button';

const IS_DEV_ENVIRONMENT = typeof process !== 'undefined' ? process.env.NODE_ENV === 'development' : (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'));

interface Props {
  children: ReactNode;
  /** Overridable in tests to avoid dependency on window.location.reload */
  onReload?: () => void;
  /** Overridable in tests to avoid dependency on window.location.href */
  onGoHome?: () => void;
}

interface State {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
    errorInfo: null,
  };

  public static getDerivedStateFromError(error: Error): Partial<State> {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    this.setState({ errorInfo });
    this.logErrorToBackend(error, errorInfo);
  }

  private async logErrorToBackend(error: Error, errorInfo: ErrorInfo) {
    try {
      const componentName = this.extractComponentName(errorInfo.componentStack);
      const requestUri = window.location.pathname + window.location.search;
      const payload = {
        message: error.message || 'React UI Crash',
        stack: error.stack || errorInfo.componentStack || '',
        component: componentName,
        requestUri: requestUri || '/',
        metadata: {
          userAgent: navigator.userAgent,
          language: navigator.language,
          timestamp: new Date().toISOString(),
          componentStack: errorInfo.componentStack,
          url: window.location.href,
        },
      };
      await api.post('/api/v1/logs/client', payload);
    } catch (err) {
      console.error('Failed to report frontend error to backend', err);
    }
  }

  private extractComponentName(componentStack?: string | null): string {
    if (!componentStack) return 'UnknownComponent';
    const match = componentStack.match(/at\s+([A-Z][A-Za-z0-9_]*)/);
    return match ? match[1] : 'ReactComponent';
  }

  private handleReload = () => {
    if (this.props.onReload) {
      this.props.onReload();
    } else {
      window.location.reload();
    }
  };

  private handleGoHome = () => {
    if (this.props.onGoHome) {
      this.props.onGoHome();
    } else {
      window.location.href = '/';
    }
  };

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-background p-4 sm:p-6 lg:p-8">
          <div className="max-w-md w-full space-y-8 p-8 bg-card rounded-2xl shadow-glow border border-border/50 text-center animate-fade-in">
            <div className="flex justify-center">
              <div className="p-4 bg-destructive/10 rounded-full text-destructive animate-pulse">
                <AlertTriangle className="h-12 w-12" />
              </div>
            </div>
            <div className="space-y-3">
              <h1 className="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-destructive to-purple-600 font-heading">
                Oops! Algo deu errado
              </h1>
              <p className="text-muted-foreground text-sm leading-relaxed">
                Desculpe pelo transtorno. Ocorreu uma falha inesperada na interface.
                Nossa equipe técnica já foi notificada automaticamente e está trabalhando nisso.
              </p>
            </div>
            <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
              <Button
                onClick={this.handleReload}
                className="bg-primary hover:bg-primary/90 text-white font-semibold shadow-md transition-all duration-300 flex items-center justify-center gap-2"
              >
                <RefreshCw className="h-4 w-4" />
                Recarregar Página
              </Button>
              <Button
                variant="outline"
                onClick={this.handleGoHome}
                className="hover:bg-accent hover:text-accent-foreground border-dashed transition-all duration-300 flex items-center justify-center gap-2"
              >
                <Home className="h-4 w-4" />
                Ir para o Início
              </Button>
            </div>
            {IS_DEV_ENVIRONMENT && this.state.error && (
              <div className="mt-6 text-left bg-muted/50 p-4 rounded-lg border border-border overflow-auto max-h-48 text-xs font-mono">
                <p className="font-bold text-destructive mb-1">{this.state.error.toString()}</p>
                {this.state.errorInfo?.componentStack && (
                  <pre className="text-muted-foreground whitespace-pre-wrap text-[10px]">
                    {this.state.errorInfo.componentStack}
                  </pre>
                )}
              </div>
            )}
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
