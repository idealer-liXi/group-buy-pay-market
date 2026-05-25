import { describe, expect, it } from 'vitest'
import { injectPayFormHtml } from './pay'

describe('pay helper', () => {
  it('injects returned form html into the document body', () => {
    injectPayFormHtml('<form id="pay-form"></form>')
    expect(document.getElementById('pay-form')).not.toBeNull()
  })
})
