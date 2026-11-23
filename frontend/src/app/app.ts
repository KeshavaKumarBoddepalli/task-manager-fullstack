import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, finalize } from 'rxjs';
import { Task, TaskRequest, TaskStatus } from './tasks/task.model';
import { TaskService } from './tasks/task.service';

interface TaskForm {
  title: string;
  description: string;
  status: TaskStatus;
}

function emptyTaskForm(): TaskForm {
  return {
    title: '',
    description: '',
    status: 'PENDING',
  };
}

@Component({
  imports: [DatePipe, FormsModule],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly taskService = inject(TaskService);

  protected readonly tasks = signal<Task[]>([]);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly processingTaskId = signal<number | null>(null);
  protected readonly errorMessage = signal('');
  protected readonly editingTaskId = signal<number | null>(null);
  protected readonly pendingCount = computed(
    () => this.tasks().filter((task) => task.status === 'PENDING').length,
  );
  protected readonly inProgressCount = computed(
    () => this.tasks().filter((task) => task.status === 'IN_PROGRESS').length,
  );
  protected readonly completedCount = computed(
    () => this.tasks().filter((task) => task.status === 'COMPLETED').length,
  );
  protected readonly statuses: TaskStatus[] = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];

  protected searchTerm = '';
  protected selectedStatus: TaskStatus | '' = '';
  protected form = emptyTaskForm();

  ngOnInit(): void {
    this.loadTasks();
  }

  protected loadTasks(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.taskService
      .getTasks({
        status: this.selectedStatus || undefined,
        search: this.searchTerm.trim() || undefined,
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (tasks) => this.tasks.set(tasks),
        error: (error: unknown) => this.errorMessage.set(this.describeError(error)),
      });
  }

  protected applyFilters(): void {
    this.loadTasks();
  }

  protected clearFilters(): void {
    this.searchTerm = '';
    this.selectedStatus = '';
    this.loadTasks();
  }

  protected saveTask(): void {
    const taskId = this.editingTaskId();
    const request: TaskRequest = {
      title: this.form.title.trim(),
      description: this.form.description.trim() || null,
      status: this.form.status,
    };
    const saveRequest =
      taskId === null
        ? this.taskService.createTask(request)
        : this.taskService.updateTask(taskId, request);

    this.saving.set(true);
    this.errorMessage.set('');
    saveRequest
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.cancelEdit();
          this.loadTasks();
        },
        error: (error: unknown) => this.errorMessage.set(this.describeError(error)),
      });
  }

  protected editTask(task: Task): void {
    this.editingTaskId.set(task.id);
    this.form = {
      title: task.title,
      description: task.description ?? '',
      status: task.status,
    };
    this.errorMessage.set('');
  }

  protected cancelEdit(): void {
    this.editingTaskId.set(null);
    this.form = emptyTaskForm();
  }

  protected completeTask(task: Task): void {
    this.runTaskAction(
      task.id,
      this.taskService.completeTask(task.id),
    );
  }

  protected deleteTask(task: Task): void {
    if (!window.confirm(`Delete "${task.title}"? This cannot be undone.`)) {
      return;
    }

    this.runTaskAction(task.id, this.taskService.deleteTask(task.id));
  }

  protected statusLabel(status: TaskStatus): string {
    return status.replace('_', ' ');
  }

  private runTaskAction(taskId: number, action: Observable<unknown>): void {
    this.processingTaskId.set(taskId);
    this.errorMessage.set('');
    action
      .pipe(finalize(() => this.processingTaskId.set(null)))
      .subscribe({
        next: () => this.loadTasks(),
        error: (error: unknown) => this.errorMessage.set(this.describeError(error)),
      });
  }

  private describeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (
        typeof error.error === 'object' &&
        error.error !== null &&
        'message' in error.error &&
        typeof error.error.message === 'string'
      ) {
        return error.error.message;
      }

      if (error.status === 0) {
        return 'Could not reach the API. Check that the backend is running at http://localhost:8080.';
      }

      return `The request failed with status ${error.status}. Please try again.`;
    }

    return 'Something went wrong. Please try again.';
  }
}
