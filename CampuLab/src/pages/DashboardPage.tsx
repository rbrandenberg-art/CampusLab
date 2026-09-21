import { useMsal } from "@azure/msal-react";
import { getUserRoles } from "../auth/roles";

export default function DashboardPage() {
    const { accounts } = useMsal();
    const roles = getUserRoles(accounts[0]);

    // 👉 IMPRIME LOS DATOS AQUÍ:
    console.log("Claims desde Azure:", accounts[0]?.idTokenClaims);
    console.log("Roles mapeados en React:", roles);

    return (
        <div>
            <h2>Dashboard</h2>

            {roles.includes("ADMIN") && (
                <section>
                    <h3>Ocupación de labs</h3>
                    <p>Vista de ocupación en tiempo real de los 20 laboratorios (pendiente de datos del BFF).</p>
                </section>
            )}

            {roles.includes("TECNICO") && (
                <section>
                    <h3>Reservas por preparar</h3>
                    <p>Listado de reservas aprobadas que requieren preparación de sala/equipo.</p>
                </section>
            )}

            {roles.includes("ESTUDIANTE") && (
                <section>
                    <h3>Tus próximas reservas</h3>
                    <p>Estado de tus solicitudes de reserva.</p>
                </section>
            )}

            {roles.includes("AUDITOR") && (
                <section>
                    <h3>Resumen de auditoría</h3>
                    <p>Acceso de solo lectura al timeline de eventos.</p>
                </section>
            )}
        </div>
    );
}
