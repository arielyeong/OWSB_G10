=========================
System Flow Overview
=========================

The OWSB Purchase Order Management System is designed to streamline procurement and inventory processes for Omega Wholesale Sdn Bhd. Below is the system flow:

=========================
Login & Role-Based Access
=========================

Users (Admin, Sales Manager, Purchase Manager, Inventory Manager, Finance Manager) log in with credentials.

Role determines accessible features (e.g., Admin manages users; Finance Manager approves payments).

Administrator
=========================

Add/Edit/Delete Users: Registers new users (auto-generates IDs) and modifies roles.

Search Users: Finds users by ID.

Sales Manager
=========================

Item Management: Adds/edits items (auto-generated ID, category selection, multi-supplier linking).

Supplier Management: Registers suppliers with bank/email validation.

Daily Sales: Records sales, auto-updates stock.

Purchase Requisition (PR): Creates PRs, links items/suppliers, and submits for approval.

Purchase Manager
=========================

Approve/Reject PRs: Updates PR status.

Generate Purchase Order (PO): Converts approved PRs to POs.

Edit/Delete POs: Manages dates (Order/Delivery/Invoice) and supplier details.

Inventory Manager
=========================

Update Stock: Confirms received items from POs, adjusts inventory.

Low Stock Alerts: Flags items below threshold.

Generate Reports: Exports stock summaries.

Finance Manager
=========================

Approve POs: Validates POs for payment.

Process Payments: Selects payment method (bank transfer/cash), records transactions.

Financial Reports: Generates payment history.

====================
Key Features
====================

Auto-generated IDs (Items, PRs, POs).

Validation: Non-negative numbers, mandatory fields, email/bank checks.

Cascading actions: Deleting a PR auto-deletes linked PO.

Role-specific GUIs with navigation buttons (First/Prev/Next/Last).


====================
Output Files
====================

item.txt, supplier.txt: Store item/supplier records.

supplieritem.txt: Manages item-supplier relationships.

Reports: Generated as .txt files (e.g., financial reports).

Note: Ensure all inputs meet validation rules (e.g., valid dates: Order ≤ Delivery ≤ Invoice). Use "Clear" buttons to reset forms.