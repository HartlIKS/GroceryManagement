import { Component, input } from '@angular/core';
import { FieldTree, FormField } from '@angular/forms/signals';
import { MatFormField, MatInput, MatLabel } from '@angular/material/input';

@Component({
  selector: 'basic-paging-form-part',
  imports: [
    MatFormField,
    MatInput,
    MatLabel,
    FormField
  ],
  templateUrl: './basic-paging-form-part.component.html',
  styleUrl: './basic-paging-form-part.component.css',
})
export class BasicPagingFormPart {
  readonly formInput = input.required<{
    page: FieldTree<number>,
    pageSize: FieldTree<number>,
    itemCount: FieldTree<number>,
  }>();
}
