import React, { ReactNode } from "react";
import { GoogleReCaptchaContext } from "react-google-recaptcha-v3";

interface MockGoogleReCaptchaProviderProps {
    children: ReactNode;
}

export const MockGoogleReCaptchaProvider: React.FC<MockGoogleReCaptchaProviderProps> = ({ children }) => {
    const executeRecaptcha = async (action?: string) => {
        console.log(`[MockReCaptcha] executeRecaptcha called with action: ${action}`);
        return "mock-recaptcha-token";
    };

    return (
        <GoogleReCaptchaContext.Provider
            value={{
                executeRecaptcha,
                container: "mock-container",
            }}
        >
            {children}
        </GoogleReCaptchaContext.Provider>
    );
};
