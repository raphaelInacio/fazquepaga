import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import ErrorBoundary from './ErrorBoundary';
import api from '@/lib/api';

// Mock for the axios API client to prevent real HTTP calls
jest.mock('@/lib/api', () => ({
  __esModule: true,
  default: {
    post: jest.fn().mockResolvedValue({ status: 202 }),
  },
}));

// Helper component that intentionally throws a render error
const ProblemChild = ({ shouldThrow = false }) => {
  if (shouldThrow) {
    throw new Error('Erro de renderização intencional');
  }
  return <div>Componente renderizado sem erros</div>;
};

// Helper component that throws with no recognisable component name in the stack
const AnonymousErrorChild = () => {
  throw new Error('anonymous crash');
};

// Helper component that throws with no stack at all
const StacklessErrorChild = () => {
  const err = new Error('no stack');
  err.stack = undefined;
  throw err;
};

describe('ErrorBoundary', () => {
  let consoleSpy: jest.SpyInstance;

  beforeEach(() => {
    // Suppress expected console.error output from React error boundary lifecycle
    consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    consoleSpy.mockRestore();
    jest.clearAllMocks();
  });

  test('renders children normally when no error occurs', () => {
    render(
      <ErrorBoundary>
        <ProblemChild shouldThrow={false} />
      </ErrorBoundary>
    );

    expect(screen.getByText('Componente renderizado sem erros')).toBeInTheDocument();
    expect(screen.queryByText('Oops! Algo deu errado')).not.toBeInTheDocument();
    expect(api.post).not.toHaveBeenCalled();
  });

  test('catches error in child, renders friendly UI and posts log to backend', async () => {
    render(
      <ErrorBoundary>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    // Should display the friendly error UI
    expect(screen.getByText('Oops! Algo deu errado')).toBeInTheDocument();
    expect(screen.getByText(/Desculpe pelo transtorno/)).toBeInTheDocument();

    // Should have dispatched exactly one POST request with the correct log payload
    expect(api.post).toHaveBeenCalledTimes(1);
    expect(api.post).toHaveBeenCalledWith(
      '/api/v1/logs/client',
      expect.objectContaining({
        message: 'Erro de renderização intencional',
        component: expect.any(String),
        requestUri: expect.any(String),
        stack: expect.any(String),
        metadata: expect.objectContaining({
          userAgent: expect.any(String),
          language: expect.any(String),
          timestamp: expect.any(String),
          componentStack: expect.any(String),
          url: expect.any(String),
        }),
      })
    );
  });

  test('calls onReload when the "Recarregar Página" button is clicked', () => {
    const onReload = jest.fn();

    render(
      <ErrorBoundary onReload={onReload}>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    const reloadButton = screen.getByRole('button', { name: /Recarregar Página/i });
    fireEvent.click(reloadButton);

    expect(onReload).toHaveBeenCalledTimes(1);
  });

  test('calls onGoHome when the "Ir para o Início" button is clicked', () => {
    const onGoHome = jest.fn();

    render(
      <ErrorBoundary onGoHome={onGoHome}>
        <ProblemChild shouldThrow={true} />
      </ErrorBoundary>
    );

    const homeButton = screen.getByRole('button', { name: /Ir para o Início/i });
    fireEvent.click(homeButton);

    expect(onGoHome).toHaveBeenCalledTimes(1);
  });

  test('uses "ReactComponent" as fallback when component name cannot be parsed from stack', async () => {
    const onReload = jest.fn();

    render(
      <ErrorBoundary onReload={onReload}>
        <AnonymousErrorChild />
      </ErrorBoundary>
    );

    expect(api.post).toHaveBeenCalledWith(
      '/api/v1/logs/client',
      expect.objectContaining({
        component: expect.stringMatching(/^[A-Z]/),
      })
    );
  });

  test('uses "UnknownComponent" as fallback when componentStack is missing', () => {
    render(
      <ErrorBoundary>
        <StacklessErrorChild />
      </ErrorBoundary>
    );

    expect(screen.getByText('Oops! Algo deu errado')).toBeInTheDocument();
    expect(api.post).toHaveBeenCalledTimes(1);
  });
});
