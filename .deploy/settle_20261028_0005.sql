DO $settle$
DECLARE
    v_job_id  text;
    v_cust_id text;
    v_user_id text := '9ce1c440-e311-4f7a-a48c-ef69a5b9b50c';
    v_inv_id  text := gen_random_uuid()::text;
    v_je_id   text := gen_random_uuid()::text;
    v_ar_id   text := '73f9f7ef-802d-4a42-81d9-978b2a460889';
    v_rev_hw  text := '26617e23-be90-4bd6-a1d6-5fc0fc9e99f9';
    v_rev_lab text := '7ebca875-bec3-4998-aeb7-86f1d14b8359';
    v_vat     text := '5b8ce6a3-a3ec-43b8-b239-b511416af035';
    v_num     text := 'INV-20261028-0005';
BEGIN
    SELECT id, "customerId" INTO v_job_id, v_cust_id 
    FROM "RepairJob" WHERE "jobNumber" = 'JOB-20261028-0005' LIMIT 1;

    IF NOT EXISTS (SELECT 1 FROM "Invoice" WHERE "invoiceNo" = v_num) THEN
        INSERT INTO "Invoice" (
            "id", "invoiceNo", "customerId", "subTotal", "taxAmount", 
            "discount", "grandTotal", "paidAmount", "isPaid", "userId", "notes"
        ) VALUES (
            v_inv_id, v_num, v_cust_id, 7500.00, 975.00, 
            0.00, 8475.00, 8475.00, TRUE, v_user_id, 
            'Settled Job JOB-20261028-0005: Samsung 980 NVMe SSD + Labor'
        );

        INSERT INTO "JournalEntry" ("id", "entryNumber", "reference", "memo", "userId", "date")
        VALUES (v_je_id, 'JE-' || v_num, v_num, 'Invoice settlement for Job JOB-20261028-0005', v_user_id, CURRENT_TIMESTAMP);

        INSERT INTO "JournalLine" ("id", "journalEntryId", "debitAccountId", "creditAccountId", "amount")
        VALUES 
            (gen_random_uuid()::text, v_je_id, v_ar_id, v_rev_hw, 5800.00),
            (gen_random_uuid()::text, v_je_id, v_ar_id, v_rev_lab, 1700.00),
            (gen_random_uuid()::text, v_je_id, v_ar_id, v_vat, 975.00);
    END IF;
END $settle$;
