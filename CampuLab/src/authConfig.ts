import type { Configuration } from "@azure/msal-browser";
import { LogLevel } from "@azure/msal-browser";

export const msalConfig: Configuration = {
    auth: {
        clientId: import.meta.env.VITE_AZURE_CLIENT_ID,
        authority: `https://login.microsoftonline.com/${import.meta.env.VITE_AZURE_TENANT_ID}`,
        redirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI,
    },
    cache: {
        cacheLocation: "localStorage",
    },
    system: {
        loggerOptions: {
        loggerCallback: (level, message, containsPii) => {
            if (containsPii) return;
            if (level === LogLevel.Error) console.error(message);
        },
        logLevel: LogLevel.Error,
        },
    },
};

export const loginRequest = {
    scopes: ["api://12b62be7-f3e9-4258-8731-780bb8dda6a7/access_as_user"],
};