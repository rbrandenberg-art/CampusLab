import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { apiFetch } from "../api/client";

export default function AuditPage() {
    const { instance, accounts } = useMsal();
    const [data, setData] = useState<unknown>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            setError(null);
            try {
                const res = await apiFetch("/api/audit/events", { instance, account: accounts[0] });
                const json = await res.json();
                if (!cancelled) setData(json);
            } catch (err) {
                if (!cancelled) setError(err instanceof Error ? err.message : "Error desconocido");
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => {
            cancelled = true;
        };
    }, [instance, accounts]);

    return (
        <div>
            <h2>Auditoría</h2>
            {loading && <p>Cargando eventos de auditoría...</p>}
            {error && (
                <p style={{ color: "crimson" }}>
                    No se pudo cargar auditoría ({error}). Verifica que ms-campuslab-audit esté disponible detrás del BFF.
                </p>
            )}
            {!loading && !error && <pre>{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}
