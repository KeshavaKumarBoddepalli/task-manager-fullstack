import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Task, TaskFilters, TaskRequest } from './task.model';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly tasksUrl = `${environment.apiBaseUrl}/tasks`;

  getTasks(filters: TaskFilters = {}): Observable<Task[]> {
    let params = new HttpParams();
    if (filters.status) {
      params = params.set('status', filters.status);
    }
    if (filters.search) {
      params = params.set('search', filters.search);
    }

    return this.http.get<Task[]>(this.tasksUrl, { params });
  }

  createTask(request: TaskRequest): Observable<Task> {
    return this.http.post<Task>(this.tasksUrl, request);
  }

  updateTask(id: number, request: TaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.tasksUrl}/${id}`, request);
  }

  completeTask(id: number): Observable<Task> {
    return this.http.patch<Task>(`${this.tasksUrl}/${id}/complete`, {});
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.tasksUrl}/${id}`);
  }
}
