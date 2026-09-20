import type { AccountInfo, IPublicClientApplication } from "@azure/msal-browser";
import { InteractionRequiredAuthError } from "@azure/msal-browser";
import { loginRequest } from "../authConfig";

// URL del BFF (ms-campuslab-bff), que enruta a los microservicios de dominio.
// Default de desarrollo: http://localhost:8080. Ajustar via VITE_BFF_URL (.env)
// segun el puerto que reporte finalmente ese servicio.
const BFF_URL = import.meta.env.VITE_BFF_URL || "http://localhost:8080";

export interface ApiFetchOptions extends RequestInit {
    instance: IPublicClientApplication;
    account?: AccountInfo | null;
}

/**
 * Llama a un endpoint del BFF adjuntando el Bearer token de MSAL.
 * `path` es relativo (ej. "/api/bookings"); se concatena con VITE_BFF_URL.
 */
export async function apiFetch(path: string, options: ApiFetchOptions): Promise<Response> {
    const { instance, account, ...fetchOptions } = options;
    const activeAccount = account ?? instance.getActiveAccount();

    if (!activeAccount) {
        throw new Error("No hay una cuenta activa");
    }

    let accessToken: string;
    try {
        const result = await instance.acquireTokenSilent({
            ...loginRequest,
            account: activeAccount,
        });
        accessToken = result.accessToken;
    } catch (error) {
        if (error instanceof InteractionRequiredAuthError) {
            await instance.acquireTokenRedirect(loginRequest);
            throw new Error("Redirigiendo para autenticación", { cause: error });
        }
        throw error;
    }

    const response = await fetch(`${BFF_URL}${path}`, {
        ...fetchOptions,
        headers: {
            ...(fetchOptions.headers || {}),
            Authorization: `Bearer ${accessToken}`,
        },
    });

    if (!response.ok) {
        throw new Error(`Error ${response.status} al llamar ${path}`);
    }

    return response;
}
