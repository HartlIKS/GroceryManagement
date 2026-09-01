export type ModeSwitch = {
  create: {},
  update: {},
  list: {},
};

export type Mode = keyof ModeSwitch;

export type CREATE = 'create';
export type UPDATE = 'update';
export type LIST = 'list';

export type Always<T> = {
  create: T,
  update: T,
  list: T,
};

export type CreateOnly<T> = {
  create: T,
  update: {},
  list: {},
}

export type UpdateOnly<T> = {
  create: {},
  update: T,
  list: {},
};

export type ListOnly<T> = {
  create: {},
  update: {},
  list: T,
}

export type NotCreate<T> = {
  create: {},
  update: T,
  list: T,
};

export type NotUpdate<T> = {
  create: T,
  update: {},
  list: T,
};

export type NotList<T> = {
  create: T,
  update: T,
  list: {},
};

export type BaseDTOTypes = ListOnly<{uuid: string}> & NotCreate<{version: number}>;
