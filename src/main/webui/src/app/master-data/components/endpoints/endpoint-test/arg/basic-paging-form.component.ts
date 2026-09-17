import { Component, input, output, viewChild } from '@angular/core';
import { EndpointDTOTypes } from '../../../../models';
import { LIST } from '../../../../../models/base.model';
import { EndpointFormComponent } from './endpoint-form';
import { FormRoot } from '@angular/forms/signals';
import { ReactiveFormsModule } from '@angular/forms';
import { BasicPagingFormPart } from './parts/basic-paging-form-part.component';
import { MatButton } from '@angular/material/button';

@Component({
  imports: [
    FormRoot,
    ReactiveFormsModule,
    BasicPagingFormPart,
    MatButton
  ],
  templateUrl: './basic-paging-form.component.html',
  styleUrl: './basic-paging-form.component.css',
  standalone: true,
})
export class BasicPagingFormComponent<T extends EndpointDTOTypes[LIST]> extends EndpointFormComponent<T> {
  readonly api = input.required<string>();
  readonly endpoint = input.required<T>();
  readonly submit = output<{
    headers?: Record<string, string[]>,
    params?: Record<string, string[]>,
  }>();
  protected readonly htmlForm = viewChild.required(HTMLFormElement);

  constructor() {
    super({}, p => p);
  }
}
