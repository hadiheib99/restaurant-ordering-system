import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User, UserRequest } from '../models/user';

/** Administrator REST client for restaurant user-account management. */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/users';

  /** @returns all managed user accounts */
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  /** @param request new account data @returns created user */
  createUser(request: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, request);
  }

  /** @param id user identifier @param request updated account data @returns updated user */
  updateUser(id: number, request: UserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request);
  }

  /** @param id user identifier @param value enabled state @returns updated user */
  setEnabled(id: number, value: boolean): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/${id}/enabled?value=${value}`, {});
  }

  /** @param id user identifier @returns completion observable for deletion */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
