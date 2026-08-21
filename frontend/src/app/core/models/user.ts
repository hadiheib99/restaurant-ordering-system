export type UserRole = 'ADMIN' | 'WAITER' | 'CUSTOMER' | 'CHEF';

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

export interface UserRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
}
