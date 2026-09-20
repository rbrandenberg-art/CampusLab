import { useMsal, useIsAuthenticated } from "@azure/msal-react";
import { InteractionStatus } from "@azure/msal-browser";
import { Navigate } from "react-router-dom";
import { loginRequest } from "../authConfig";

export default function LoginPage() {
    const { instance, inProgress } = useMsal();
    const isAuthenticated = useIsAuthenticated();

    if (isAuthenticated) {
        return <Navigate to="/dashboard" replace />;
    }

    const handleLogin = () => {
        if (inProgress === InteractionStatus.None) {
            instance.loginRedirect(loginRequest).catch((e) => console.error(e));
        }
    };

    return (
        <div style={{ padding: "2rem", fontFamily: "sans-serif" }}>
            <h1>Portal de Autenticación con Microsoft Entra ID</h1>
            <p>Debes iniciar sesión con tu cuenta institucional para continuar.</p>
            <button onClick={handleLogin} disabled={inProgress !== InteractionStatus.None}>
                {inProgress !== InteractionStatus.None
                    ? "Cargando..."
                    : "Iniciar sesión con Microsoft"}
            </button>
        </div>
    );
}
