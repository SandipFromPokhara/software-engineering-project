import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib.pagesizes import A4
import os

CSV_FILE = "load-test-results.csv"
OUTPUT_PDF = "load_test_report.pdf"

# ---------------------------
# Load data
# ---------------------------
df = pd.read_csv(CSV_FILE)
latency = df["latency_ms"].values

# ---------------------------
# Stats
# ---------------------------
avg = np.mean(latency)
min_v = np.min(latency)
max_v = np.max(latency)
p95 = np.percentile(latency, 95)
p99 = np.percentile(latency, 99)

# ---------------------------
# Create plots folder
# ---------------------------
os.makedirs("plots", exist_ok=True)

# ---------------------------
# 1. Latency over time
# ---------------------------
plt.figure()
plt.plot(latency)
plt.title("Latency Over Time")
plt.xlabel("Request Index")
plt.ylabel("Latency (ms)")
plt.grid(True)
plt.savefig("plots/latency_over_time.png")
plt.close()

# ---------------------------
# 2. Histogram
# ---------------------------
plt.figure()
plt.hist(latency, bins=30)
plt.title("Latency Distribution")
plt.xlabel("Latency (ms)")
plt.ylabel("Frequency")
plt.grid(True)
plt.savefig("plots/histogram.png")
plt.close()

# ---------------------------
# 3. CDF
# ---------------------------
sorted_lat = np.sort(latency)
cdf = np.arange(len(sorted_lat)) / len(sorted_lat)

plt.figure()
plt.plot(sorted_lat, cdf)
plt.title("Latency CDF")
plt.xlabel("Latency (ms)")
plt.ylabel("Percentile")
plt.grid(True)

# mark P95 and P99
plt.axvline(p95, color='orange', linestyle='--', label=f'P95={p95:.2f}')
plt.axvline(p99, color='red', linestyle='--', label=f'P99={p99:.2f}')
plt.legend()

plt.savefig("plots/cdf.png")
plt.close()

# ---------------------------
# PDF Report
# ---------------------------
doc = SimpleDocTemplate(OUTPUT_PDF, pagesize=A4)
styles = getSampleStyleSheet()
content = []

# Title
content.append(Paragraph("Load Test Performance Report", styles["Title"]))
content.append(Spacer(1, 12))

# Stats section
stats_text = f"""
<b>Summary Statistics</b><br/>
Average: {avg:.2f} ms<br/>
Min: {min_v:.2f} ms<br/>
Max: {max_v:.2f} ms<br/>
P95: {p95:.2f} ms<br/>
P99: {p99:.2f} ms<br/>
"""
content.append(Paragraph(stats_text, styles["Normal"]))
content.append(Spacer(1, 12))

# Images
content.append(Paragraph("Latency Over Time", styles["Heading2"]))
content.append(Image("plots/latency_over_time.png", width=450, height=200))
content.append(Spacer(1, 12))

content.append(Paragraph("Latency Distribution", styles["Heading2"]))
content.append(Image("plots/histogram.png", width=450, height=200))
content.append(Spacer(1, 12))

content.append(Paragraph("Latency CDF", styles["Heading2"]))
content.append(Image("plots/cdf.png", width=450, height=200))

# Build PDF
doc.build(content)

print("✅ Report generated: load_test_report.pdf")