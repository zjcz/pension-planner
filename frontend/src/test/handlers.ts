import { http, HttpResponse } from 'msw';
import {
  analytics,
  appInfo,
  authConfig,
  dashboard,
  otherIncomeItems,
  pensions,
  settings,
  statePension,
  statements,
  tags,
  user,
} from './fixtures';
import { resetFixtures } from './fixtures';

const V1 = '/api/v1';

let nextPensionId = 3;
let nextTagId = 3;
let nextOiId = 3;

export function resetHandlersState() {
  nextPensionId = 3;
  nextTagId = 3;
  nextOiId = 3;
  resetFixtures();
}

export const handlers = [
  http.get(`${V1}/info`, () => HttpResponse.json(appInfo)),
  http.get(`${V1}/auth/config`, () => HttpResponse.json(authConfig)),
  http.get(`${V1}/auth/me`, () => HttpResponse.json(user)),
  http.post(`${V1}/auth/logout`, () => new HttpResponse(null, { status: 200 })),

  http.get(`${V1}/settings`, () => HttpResponse.json(settings)),
  http.put(`${V1}/settings`, async ({ request }) => {
    const body = (await request.json()) as any;
    Object.assign(settings, body);
    return HttpResponse.json(settings);
  }),

  http.get(`${V1}/tags`, () => HttpResponse.json(tags)),
  http.post(`${V1}/tags`, async ({ request }) => {
    const body = (await request.json()) as { name: string };
    const created = { id: nextTagId++, name: body.name };
    tags.push(created);
    return HttpResponse.json(created);
  }),
  http.put(`${V1}/tags/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as { name: string };
    const tag = tags.find((t) => t.id === id);
    if (tag) tag.name = body.name;
    return HttpResponse.json(tag);
  }),
  http.delete(`${V1}/tags/:id`, ({ params }) => {
    const id = Number(params.id);
    const idx = tags.findIndex((t) => t.id === id);
    if (idx >= 0) tags.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${V1}/pensions`, () => HttpResponse.json(pensions)),
  http.get(`${V1}/pensions/:id`, ({ params }) => {
    const pension = pensions.find((p) => p.pensionId === Number(params.id));
    if (!pension) return HttpResponse.json(null, { status: 404 });
    return HttpResponse.json(pension);
  }),
  http.post(`${V1}/pensions`, async ({ request }) => {
    const body = (await request.json()) as any;
    const created = {
      pensionId: nextPensionId++,
      name: body.name,
      maturityDate: body.maturityDate,
      notes: body.notes,
      status: body.status,
      statusDate: '2026-09-01',
      color: body.color,
      tags: tags.filter((t) => body.tagIds?.includes(t.id)),
      providerName: body.providerName,
      policyNumber: body.policyNumber,
      workplaceName: body.workplaceName,
    };
    pensions.unshift(created);
    return HttpResponse.json(created);
  }),
  http.put(`${V1}/pensions/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as any;
    const pension = pensions.find((p) => p.pensionId === id);
    if (pension) {
      Object.assign(pension, {
        name: body.name,
        maturityDate: body.maturityDate,
        notes: body.notes,
        status: body.status,
        color: body.color,
        providerName: body.providerName,
        policyNumber: body.policyNumber,
        workplaceName: body.workplaceName,
        tags: tags.filter((t) => body.tagIds?.includes(t.id)),
      });
    }
    return HttpResponse.json(pension);
  }),
  http.delete(`${V1}/pensions/:id`, ({ params }) => {
    const id = Number(params.id);
    const idx = pensions.findIndex((p) => p.pensionId === id);
    if (idx >= 0) pensions.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${V1}/pensions/:id/statements`, ({ params }) =>
    HttpResponse.json(statements.filter((s) => s.pensionId === Number(params.id))),
  ),
  http.post(`${V1}/pensions/:id/statements`, async ({ params, request }) => {
    const pensionId = Number(params.id);
    const body = (await request.json()) as any;
    const created = { statementId: 99, pensionId, ...body };
    statements.push(created);
    return HttpResponse.json(created);
  }),
  http.put(`${V1}/pensions/:pensionId/statements/:statementId`, async ({ params, request }) => {
    const statementId = Number(params.statementId);
    const body = (await request.json()) as any;
    const stmt = statements.find((s) => s.statementId === statementId);
    if (stmt) Object.assign(stmt, body);
    return HttpResponse.json(stmt);
  }),
  http.delete(`${V1}/pensions/:pensionId/statements/:statementId`, ({ params }) => {
    const statementId = Number(params.statementId);
    const idx = statements.findIndex((s) => s.statementId === statementId);
    if (idx >= 0) statements.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${V1}/other-income`, () => HttpResponse.json(otherIncomeItems)),
  http.post(`${V1}/other-income`, async ({ request }) => {
    const body = (await request.json()) as any;
    const created = {
      id: nextOiId++,
      name: body.name,
      annualAmount: body.annualAmount,
      notes: body.notes,
      tags: tags.filter((t) => body.tagIds?.includes(t.id)),
    };
    otherIncomeItems.unshift(created);
    return HttpResponse.json(created);
  }),
  http.put(`${V1}/other-income/:id`, async ({ params, request }) => {
    const id = Number(params.id);
    const body = (await request.json()) as any;
    const oi = otherIncomeItems.find((o) => o.id === id);
    if (oi) {
      Object.assign(oi, {
        name: body.name,
        annualAmount: body.annualAmount,
        notes: body.notes,
        tags: tags.filter((t) => body.tagIds?.includes(t.id)),
      });
    }
    return HttpResponse.json(oi);
  }),
  http.delete(`${V1}/other-income/:id`, ({ params }) => {
    const id = Number(params.id);
    const idx = otherIncomeItems.findIndex((o) => o.id === id);
    if (idx >= 0) otherIncomeItems.splice(idx, 1);
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${V1}/state-pension`, () => HttpResponse.json(statePension)),
  http.put(`${V1}/state-pension`, async ({ request }) => {
    const body = (await request.json()) as any;
    Object.assign(statePension, body);
    return HttpResponse.json(statePension);
  }),

  http.get(`${V1}/dashboard`, () =>
    HttpResponse.json({
      ...dashboard,
      targetIncome: settings.targetIncome,
      statePension: { yearlyAmount: statePension.yearlyAmount, takesEffectYear: statePension.takesEffectYear },
      otherIncome: otherIncomeItems.map(({ id, name, annualAmount }) => ({ id, name, annualAmount })),
      retirementDate: settings.retirementDate,
    }),
  ),
  http.get(`${V1}/analytics`, () => HttpResponse.json(analytics)),
];
