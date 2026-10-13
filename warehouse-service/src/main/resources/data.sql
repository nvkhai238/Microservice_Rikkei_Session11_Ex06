INSERT INTO medicines (id,name,stock) VALUES (1,'Panadol',100) ON CONFLICT (id) DO NOTHING;
