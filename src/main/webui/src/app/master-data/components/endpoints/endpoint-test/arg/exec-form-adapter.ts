import {
  AfterViewInit,
  ComponentRef,
  computed,
  Directive,
  inject,
  input,
  inputBinding, InputSignal,
  output,
  outputBinding, OutputEmitterRef,
  signal,
  ViewContainerRef
} from '@angular/core';
import { EndpointDTOTypes } from '../../../../models';
import { LIST } from '../../../../../models/base.model';
import { ENDPOINT_TOKEN } from '../endpoint-test.component';

export interface ExecForm<T extends EndpointDTOTypes[LIST]> {
  readonly api: InputSignal<string>;
  readonly endpoint: InputSignal<T>;
  readonly submit: OutputEmitterRef<{
    headers?: Record<string, string[]>,
    params?: Record<string, string[]>,
  }>;
  nextPage(): void;
  prevPage(): void;
  resetPages(): void;
}

@Directive({
  selector: 'app-exec-form',
  standalone: true,
})
export class ExecFormAdapter implements ExecForm<EndpointDTOTypes[LIST]>, AfterViewInit {
  private readonly vcr = inject(ViewContainerRef);
  private readonly endpointConfig = inject(ENDPOINT_TOKEN);

  readonly api = input.required<string>();
  readonly endpoint = input.required<EndpointDTOTypes[LIST]>();
  readonly submit = output<{
    headers?: Record<string, string[]>,
    params?: Record<string, string[]>,
  }>();

  private readonly _instance = signal<ComponentRef<ExecForm<EndpointDTOTypes[LIST]>> | undefined>(undefined);
  readonly instance = computed(() => this._instance()?.instance);

  ngAfterViewInit() {
    const ref = this.vcr.createComponent(this.endpointConfig.formComponent, {
      bindings: [
        inputBinding("api", this.api),
        inputBinding("endpoint", this.endpoint),
        outputBinding("submit", this.submit.emit.bind(this.submit)),
      ]
    });
    ref.changeDetectorRef.detectChanges();
    this._instance.set(ref);
  }

  nextPage(): void {
    return this.instance()!.nextPage();
  }

  prevPage(): void {
    return this.instance()!.prevPage();
  }

  resetPages(): void {
    return this.instance()!.resetPages();
  }

}
