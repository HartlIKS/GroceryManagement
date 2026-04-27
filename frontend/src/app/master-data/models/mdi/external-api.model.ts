export type ExternalAPIDTO = {
  uuid: string;
  name: string;
  productMappings: Record<string, string>;
  storeMappings: Record<string, string>;
}

export type CreateExternalAPIDTO = {
  name: string;
  productMappings: Record<string, string>;
  storeMappings: Record<string, string>;
}
