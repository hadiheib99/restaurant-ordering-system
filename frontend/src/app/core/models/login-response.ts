/** Successful authentication response containing the JWT used by the client. */
export interface LoginResponse {
  /** Signed Bearer token returned by Spring Security authentication. */
  token: string;
}
