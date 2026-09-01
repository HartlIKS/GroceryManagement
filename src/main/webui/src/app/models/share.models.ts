import { Always, BaseDTOTypes, CREATE, LIST, ListOnly, Mode } from './base.model';

export type Permissions = 'NONE' | 'READ' | 'WRITE' | 'ADMIN';

export type ShareTypes = Always<{
  name: string
}> & ListOnly<{
  permissions: Permissions
}> & BaseDTOTypes;

export type Share<mode extends Mode = LIST> = ShareTypes[mode];

export type CreateShareDTO = Share<CREATE>;

export type JoinLinkTypes = Always<{
  name: string,
  permissions: Permissions,
  active: boolean,
  singleUse: boolean,
  validTo: string,
}> & ListOnly<{
  numUsers: number
}> & BaseDTOTypes;

export type JoinLink<mode extends Mode = LIST> = JoinLinkTypes[mode];

export type CreateJoinLinkDTO = JoinLink<CREATE>;
