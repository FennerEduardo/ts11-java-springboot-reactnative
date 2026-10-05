import { ApiError, COMMANDS, createTransaccionReactivaConSpringWebFluxYReactNativeClient, newTraceparent } from './transaccion-reactiva-con-spring-web-flux-y-react-native-client';

function fakeFetch(status: number, body: unknown) {
  return jest.fn(async (_url: RequestInfo | URL, _init?: RequestInit) => new Response(JSON.stringify(body), { status, headers: { 'Content-Type': 'application/json' } }));
}

describe('TransaccionReactivaConSpringWebFluxYReactNative API client', () => {
  it('exposes one method per domain command', () => {
    expect(COMMANDS).toEqual(['process_transaccion_reactiva_con_spring_web_flux_y_react_native']);
  });

  it('posts the command with tenant and idempotency headers', async () => {
    const fetch = fakeFetch(201, { type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    const client = createTransaccionReactivaConSpringWebFluxYReactNativeClient({ baseUrl: 'https://api.test/', tenantId: 'acme', fetch });

    const result = await client.processTransaccionReactivaConSpringWebFluxYReactNative('agg-1', { amount: 100 }, { idempotencyKey: 'key-1' });

    expect(result).toEqual({ type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    const [url, init] = fetch.mock.calls[0];
    expect(url).toBe('https://api.test/api/v1/transaccion-reactiva-con-spring-web-flux-y-react-native/agg-1/process_transaccion_reactiva_con_spring_web_flux_y_react_native');
    expect(init?.method).toBe('POST');
    expect((init?.headers as Record<string, string>)['X-Tenant-Id']).toBe('acme');
    expect((init?.headers as Record<string, string>)['X-Idempotency-Key']).toBe('key-1');
    expect(JSON.parse(String(init?.body))).toEqual({ amount: 100 });
  });

  it('sends a W3C traceparent that starts a new trace per request', async () => {
    const fetch = fakeFetch(201, { type: 'EmiteFlujoServerSentEventFluxConsumidoPorUseQueryEnReactNati', aggregateId: 'agg-1', version: 1 });
    const client = createTransaccionReactivaConSpringWebFluxYReactNativeClient({ fetch });

    await client.execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native');
    await client.execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native');

    const sent = fetch.mock.calls.map(([, init]) => (init?.headers as Record<string, string>).traceparent);
    for (const traceparent of sent) expect(traceparent).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
    expect(sent[0]).not.toBe(sent[1]);
    expect(newTraceparent()).toMatch(/^00-[0-9a-f]{32}-[0-9a-f]{16}-01$/);
  });

  it('propagates the caller trace context or none when disabled', async () => {
    const traced = fakeFetch(201, {});
    const parent = '00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01';
    await createTransaccionReactivaConSpringWebFluxYReactNativeClient({ fetch: traced, traceparent: () => parent }).execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native');
    expect((traced.mock.calls[0][1]?.headers as Record<string, string>).traceparent).toBe(parent);

    const untraced = fakeFetch(201, {});
    await createTransaccionReactivaConSpringWebFluxYReactNativeClient({ fetch: untraced, traceparent: false }).execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native');
    expect((untraced.mock.calls[0][1]?.headers as Record<string, string>).traceparent).toBeUndefined();
  });

  it('raises ApiError with the server detail on failure', async () => {
    const client = createTransaccionReactivaConSpringWebFluxYReactNativeClient({ fetch: fakeFetch(422, { detail: 'Command id is required' }) });
    await expect(client.execute('agg-1', 'process_transaccion_reactiva_con_spring_web_flux_y_react_native')).rejects.toEqual(new ApiError(422, 'Command id is required'));
  });
});
