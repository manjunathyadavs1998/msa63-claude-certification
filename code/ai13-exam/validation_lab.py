"""Schema checks, semantic checks and a validation-retry loop, using a scripted fake model.

No model is called. The fake returns canned extractions, so what's being tested is the loop's
decisions: which errors to feed back, and when retrying can't help.
"""
from decimal import Decimal
from typing import Literal, Optional

from pydantic import BaseModel, ValidationError, field_validator, model_validator


class LineItem(BaseModel):
    description: str
    amount: Decimal


class Invoice(BaseModel):
    invoice_number: str
    category: Literal["goods", "services", "other", "unclear"]
    category_detail: Optional[str] = None
    purchase_order: Optional[str] = None    # nullable: many invoices have none
    line_items: list[LineItem]
    stated_total: Decimal
    calculated_total: Decimal

    @field_validator("invoice_number")
    @classmethod
    def looks_like_invoice(cls, value: str) -> str:
        if not value.startswith("INV-"):
            raise ValueError("invoice_number must start with INV-, as the prompt's rule says")
        return value

    @model_validator(mode="after")
    def totals_agree(self):
        total = sum(item.amount for item in self.line_items)
        if total != self.calculated_total:
            raise ValueError(f"calculated_total is {self.calculated_total}, "
                             f"but line_items sum to {total}")
        if self.category == "other" and not self.category_detail:
            raise ValueError("category is other, so category_detail is required")
        return self


FIRST = {"invoice_number": "INV-7", "category": "goods", "stated_total": "90.00",
         "line_items": [{"description": "Valve", "amount": "40.00"},
                        {"description": "Gasket", "amount": "50.00"}],
         "calculated_total": "80.00"}
FIXED = {**FIRST, "calculated_total": "90.00"}
DISCREPANCY = {**FIRST, "stated_total": "95.00", "calculated_total": "90.00"}
NO_NUMBER = {**FIXED, "invoice_number": "UNKNOWN"}


def errors_for_prompt(error: ValidationError) -> str:
    return "; ".join(f"{'.'.join(map(str, e['loc'])) or 'document'}: {e['msg']}"
                     for e in error.errors())


def extract(replies, max_attempts=3):
    """The retry loop: on failure, the follow-up carries the document, the attempt and the errors."""
    seen = set()
    for attempt, reply in enumerate(replies[:max_attempts], start=1):
        try:
            invoice = Invoice.model_validate(reply)
        except ValidationError as error:
            feedback = errors_for_prompt(error)
            print(f"  attempt {attempt}: invalid -> {feedback}")
            if feedback in seen:
                print("  same error twice: the fix isn't in the document, so stop and route to a human")
                return None
            seen.add(feedback)
            continue
        flag = invoice.stated_total != invoice.calculated_total
        print(f"  attempt {attempt}: valid, conflict_detected={flag}")
        return invoice
    print("  attempts exhausted: route to a human")
    return None


print("A. Wrong sum, fixed by feedback")
extract([FIRST, FIXED])
print("B. The document's own total disagrees: valid output, flagged, not 'fixed'")
extract([DISCREPANCY])
print("C. The value isn't in the document: retrying repeats the error")
extract([NO_NUMBER, NO_NUMBER, NO_NUMBER])
print("D. A schema error, seen without tool use")
try:
    Invoice.model_validate({**FIXED, "category": "hardware"})
except ValidationError as error:
    print(" ", errors_for_prompt(error))
print("E. Nullable field: missing means None, not an invented value")
print("  purchase_order =", Invoice.model_validate(FIXED).purchase_order)
