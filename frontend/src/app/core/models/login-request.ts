/** Credentials submitted by the Angular login form. */
export interface LoginRequest {
  /** Account email used as the authentication principal. */
  email: string;
  /** Plain-text password sent over the login request. */
  password: string;
}
