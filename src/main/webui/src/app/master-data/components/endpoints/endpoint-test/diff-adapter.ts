import {
  AfterViewInit,
  ComponentRef,
  computed,
  Directive,
  inject,
  input,
  inputBinding,
  InputSignal,
  output,
  Signal,
  signal,
  ViewContainerRef
} from '@angular/core';
import { ENDPOINT_TOKEN } from './endpoint-test.component';

export type DiffStatus = 'loading' | 'ignored' | 'create' | 'same' | 'different';

export interface DiffComponent<T extends {} = {}> {
  readonly api: InputSignal<string>;
  readonly item: InputSignal<T & { uuid: string, name: string }>;
  readonly status: Signal<DiffStatus>;

  accept(status?: {
    create?: boolean,
    different?: boolean,
  }): any;
}

@Directive({
  selector: 'app-diff',
  standalone: true,
})
export class DiffAdapter implements DiffComponent, AfterViewInit {
  private readonly vcr = inject(ViewContainerRef);
  private readonly endpointConfig = inject(ENDPOINT_TOKEN);

  readonly api = input.required<string>();
  readonly item = input.required<{ name: string, uuid: string }>();
  readonly fetchedItem = input<{ name: string, uuid: string }>();
  readonly change = output<void>();

  private readonly _instance = signal<ComponentRef<DiffComponent> | undefined>(undefined);
  readonly instance = computed(() => this._instance()?.instance);

  readonly status = computed(() => this.instance()?.status?.() ?? 'loading');

  ngAfterViewInit(): void {
    const ref = this.vcr.createComponent(this.endpointConfig.diffComponent, {
      bindings: [
        inputBinding("api", this.api),
        inputBinding("item", this.item),
      ],
    });
    ref.changeDetectorRef.detectChanges();
    this._instance.set(ref);
  }

  accept(status?: {
    create?: boolean;
    different?: boolean;
  }) {
    return this.instance()!.accept(status);
  }
}
