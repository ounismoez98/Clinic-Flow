export const GATEWAY_URL = 'http://localhost:8085';

/** Alias used by most service files. */
export const API_BASE = GATEWAY_URL;

export const MEDICAMENTS_API = `${GATEWAY_URL}/medicaments`;
export const PATIENTS_API = `${GATEWAY_URL}/patients`;
export const ORDONNANCES_API = `${GATEWAY_URL}/ordonnances`;
export const RENDEZVOUS_API = `${GATEWAY_URL}/rendezvous`;

/** Matches pharmacy.stock.low-threshold in MSPharmacie config */
export const LOW_STOCK_THRESHOLD = 5;
