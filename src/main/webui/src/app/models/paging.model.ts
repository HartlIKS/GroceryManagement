export type PageInfo = {
  size: number,
  number: number,
  totalElements: number,
  totalPages: number,
}

export type Page<T> = {
  content: T[],
  page: PageInfo,
}
