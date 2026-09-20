import type { AccountInfo } from "@azure/msal-browser";

export type Role = "Admin" | "Tecnico" | "Estudiante" | "Auditor";

// Valor de desarrollo usado solo cuando el token no trae el claim `roles`
// (aun no existe el App Registration real con App Roles configurados en Azure AD).
const DEV_ROLE = (import.meta.env.VITE_DEV_ROLE as Role | undefined) || "Admin";

/**
 * Lee los roles del usuario desde el claim `roles` del ID token (App Roles de Azure AD).
 * Si el claim no existe todavia, hace fallback a VITE_DEV_ROLE para poder probar
 * la UI sin backend real de roles. Reemplazar este fallback cuando exista el
 * App Registration real con App Roles asignados.
 */
export function getUserRoles(account: AccountInfo | undefined | null): string[] {
    const claims = account?.idTokenClaims as { roles?: unknown } | undefined;
    const roles = claims?.roles;

    if (Array.isArray(roles) && roles.every((r) => typeof r === "string") && roles.length > 0) {
        return roles as string[];
    }

    return [DEV_ROLE];
}

export function hasAnyRole(userRoles: string[], allowedRoles: Role[]): boolean {
    return userRoles.some((role) => allowedRoles.includes(role as Role));
}
