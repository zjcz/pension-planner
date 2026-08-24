import { http } from './client';
import type { Tag, TagRequest } from '../types';

export const tagsApi = {
  list: () => http.get<Tag[]>('/tags').then((response) => response.data),

  create: (request: TagRequest) =>
    http.post<Tag>('/tags', request).then((response) => response.data),

  delete: (tagId: number) => http.delete<void>(`/tags/${tagId}`),
};
