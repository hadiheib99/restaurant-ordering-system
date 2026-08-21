import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User, UserRequest } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/users';

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  createUser(request: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, request);
  }

  updateUser(id: number, request: UserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request);
  }

  setEnabled(id: number, value: boolean): Observable<User> {
    return this.http.patch<User>(`${this.apiUrl}/${id}/enabled?value=${value}`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
