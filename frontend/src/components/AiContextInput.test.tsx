
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { AiContextInput } from './AiContextInput';

// Mock translation
jest.mock('react-i18next', () => ({
    useTranslation: () => ({
        t: (key: string) => {
            if (key === 'child.aiContext.label') return 'Bio / Interesses (IA)';
            if (key === 'child.aiContext.placeholder') return 'Ex: Gosta de dinossauros...';
            return key;
        },
    }),
}));

describe('AiContextInput', () => {
    it('renders with default label and placeholder', () => {
        render(<AiContextInput value="" onChange={() => { }} />);
        expect(screen.getByText('Bio / Interesses (IA)')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Ex: Gosta de dinossauros...')).toBeInTheDocument();
    });

    it('renders with custom label', () => {
        render(<AiContextInput value="" onChange={() => { }} label="Custom Label" />);
        expect(screen.getByText('Custom Label')).toBeInTheDocument();
    });

    it('calls onChange when typing', () => {
        const handleChange = jest.fn();
        render(<AiContextInput value="" onChange={handleChange} />);

        const textarea = screen.getByPlaceholderText('Ex: Gosta de dinossauros...');
        fireEvent.change(textarea, { target: { value: 'New context' } });

        expect(handleChange).toHaveBeenCalledWith('New context');
    });

    it('displays helper text if provided', () => {
        render(<AiContextInput value="" onChange={() => { }} helperText="Help me" />);
        expect(screen.getByText('Help me')).toBeInTheDocument();
    });
});
