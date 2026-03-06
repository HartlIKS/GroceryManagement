export type Permissions = 'NONE' | 'READ' | 'WRITE' | 'ADMIN';

export type Share = {
  uuid: string,
  name: string,
  permissions: Permissions,
}

export type CreateShareDTO = {
  name?: string,
}

export type ListShareDTO = Share;

export type JoinLink = {
  uuid: string,
  name: string,
  share: string,
}
