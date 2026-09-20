import type { ReactNode } from "react";
import { Link, Navigate, Route, Routes, useLocation } from "react-router-dom";
import { useIsAuthenticated, useMsal } from "@azure/msal-react";
import { InteractionStatus } from "@azure/msal-browser";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import BookingsPage from "./pages/BookingsPage";
import CatalogPage from "./pages/CatalogPage";
import ReportsPage from "./pages/ReportsPage";
import AuditPage from "./pages/AuditPage";
import { getUserRoles, hasAnyRole } from "./auth/roles";
import type { Role } from "./auth/roles";

function ProtectedRoute({
    allowedRoles,
    children,
}: {
    allowedRoles?: Role[];
    children: ReactNode;
}) {
    const isAuthenticated = useIsAuthenticated();
    const { accounts, inProgress } = useMsal();

    if (!isAuthenticated) {
        if (inProgress !== InteractionStatus.None) {
            return <p>Cargando sesión...</p>;
        }
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles) {
        const roles = getUserRoles(accounts[0]);
        if (!hasAnyRole(roles, allowedRoles)) {
            return <p>Sin acceso: tu rol no tiene permiso para ver esta página.</p>;
        }
    }

    return <>{children}</>;
}

interface NavItem {
    to: string;
    label: string;
    roles?: Role[];
}

const NAV_ITEMS: NavItem[] = [
    { to: "/dashboard", label: "Dashboard" },
    { to: "/bookings", label: "Reservas", roles: ["Admin", "Tecnico", "Estudiante"] },
    { to: "/catalog", label: "Catálogo", roles: ["Admin", "Tecnico"] },
    { to: "/reports", label: "Reportería", roles: ["Admin"] },
    { to: "/audit", label: "Auditoría", roles: ["Admin", "Auditor"] },
];

function Layout({ children }: { children: ReactNode }) {
    const isAuthenticated = useIsAuthenticated();
    const { instance, accounts } = useMsal();
    const location = useLocation();
    const roles = isAuthenticated ? getUserRoles(accounts[0]) : [];

    if (location.pathname === "/login") {
        return <>{children}</>;
    }

    const handleLogout = () => {
        instance.logoutRedirect({ postLogoutRedirectUri: "/" }).catch((e) => console.error(e));
    };

    return (
        <div style={{ fontFamily: "sans-serif" }}>
            {isAuthenticated && (
                <nav
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "1rem",
                        padding: "1rem 1.5rem",
                        borderBottom: "1px solid #ddd",
                    }}
                >
                    {NAV_ITEMS.filter((item) => !item.roles || hasAnyRole(roles, item.roles)).map(
                        (item) => (
                            <Link key={item.to} to={item.to}>
                                {item.label}
                            </Link>
                        ),
                    )}
                    <span style={{ marginLeft: "auto", fontSize: "0.9rem", color: "#555" }}>
                        {accounts[0]?.name || accounts[0]?.username} ({roles.join(", ")})
                    </span>
                    <button onClick={handleLogout}>Cerrar sesión</button>
                </nav>
            )}
            <div style={{ padding: "1.5rem" }}>{children}</div>
        </div>
    );
}

export default function AppRouter() {
    const isAuthenticated = useIsAuthenticated();

    return (
        <Layout>
            <Routes>
                <Route path="/login" element={<LoginPage />} />

                <Route
                    path="/dashboard"
                    element={
                        <ProtectedRoute>
                            <DashboardPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/bookings"
                    element={
                        <ProtectedRoute allowedRoles={["Admin", "Tecnico", "Estudiante"]}>
                            <BookingsPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/catalog"
                    element={
                        <ProtectedRoute allowedRoles={["Admin", "Tecnico"]}>
                            <CatalogPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/reports"
                    element={
                        <ProtectedRoute allowedRoles={["Admin"]}>
                            <ReportsPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/audit"
                    element={
                        <ProtectedRoute allowedRoles={["Admin", "Auditor"]}>
                            <AuditPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/"
                    element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />}
                />
                <Route
                    path="*"
                    element={<Navigate to={isAuthenticated ? "/dashboard" : "/login"} replace />}
                />
            </Routes>
        </Layout>
    );
}
