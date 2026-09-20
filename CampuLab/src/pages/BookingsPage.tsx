import { useEffect, useState } from "react";
import { useMsal } from "@azure/msal-react";
import { apiFetch } from "../api/client";

export default function BookingsPage() {
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
                const res = await apiFetch("/api/bookings", { instance, account: accounts[0] });
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
            <h2>Reservas</h2>
            {loading && <p>Cargando reservas...</p>}
            {error && (
                <p style={{ color: "crimson" }}>
                    No se pudo cargar reservas ({error}). Verifica que ms-campuslab-bff esté disponible.
                </p>
            )}
            {!loading && !error && <pre>{JSON.stringify(data, null, 2)}</pre>}
        </div>
    );
}
