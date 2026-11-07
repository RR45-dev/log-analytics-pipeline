import React, { useEffect, useState } from "react";

const API_BASE = process.env.REACT_APP_AGGREGATOR_API || "http://localhost:8071/api/stats";

export default function App() {
  const [stats, setStats] = useState([]);

  useEffect(() => {
    const load = () => fetch(API_BASE).then((r) => r.json()).then(setStats).catch(() => {});
    load();
    const interval = setInterval(load, 5000);
    return () => clearInterval(interval);
  }, []);

  return (
    <div style={{ fontFamily: "sans-serif", maxWidth: 800, margin: "40px auto" }}>
      <h1>Log Analytics Dashboard</h1>
      <p>Live error rates per service, aggregated from the Kafka log stream.</p>
      <table border="1" cellPadding="8" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr><th>Service</th><th>Total requests (window)</th><th>Errors</th><th>Error rate</th></tr>
        </thead>
        <tbody>
          {stats.map((s) => (
            <tr key={s.service} style={{ background: s.errorRate > 0.1 ? "#ffe5e5" : undefined }}>
              <td>{s.service}</td>
              <td>{s.totalRequests}</td>
              <td>{s.errorCount}</td>
              <td>{(s.errorRate * 100).toFixed(1)}%</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
