/** Application roles encoded in JWTs and returned by the backend. */
export type UserRole = 'ADMIN' | 'WAITER' | 'CUSTOMER' | 'CHEF';

/** Password-free user profile returned by the REST API. */
export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  role: UserRole;
  enabled: boolean;
  createdAt: string;
}

/** Editable account data submitted by the administrator user-management page. */
export interface UserRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
}
