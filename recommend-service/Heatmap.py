import numpy as np
import matplotlib.pyplot as plt

# 相似度矩阵数据（10×10）
sim_matrix = np.array([
    [1.00, 0.12, 0.05, 0.85, 0.20, 0.03, 0.41, 0.09, 0.11, 0.07],
    [0.12, 1.00, 0.33, 0.15, 0.07, 0.62, 0.18, 0.22, 0.05, 0.29],
    [0.05, 0.33, 1.00, 0.08, 0.44, 0.19, 0.06, 0.51, 0.27, 0.14],
    [0.85, 0.15, 0.08, 1.00, 0.13, 0.02, 0.37, 0.10, 0.09, 0.04],
    [0.20, 0.07, 0.44, 0.13, 1.00, 0.11, 0.22, 0.48, 0.31, 0.18],
    [0.03, 0.62, 0.19, 0.02, 0.11, 1.00, 0.08, 0.16, 0.09, 0.35],
    [0.41, 0.18, 0.06, 0.37, 0.22, 0.08, 1.00, 0.14, 0.07, 0.12],
    [0.09, 0.22, 0.51, 0.10, 0.48, 0.16, 0.14, 1.00, 0.23, 0.19],
    [0.11, 0.05, 0.27, 0.09, 0.31, 0.09, 0.07, 0.23, 1.00, 0.08],
    [0.07, 0.29, 0.14, 0.04, 0.18, 0.35, 0.12, 0.19, 0.08, 1.00]
])

plt.figure(figsize=(10, 8))
ax = plt.gca()

# 绘制热力图，选择颜色映射（例如'Reds'或'coolwarm'）
im = ax.imshow(sim_matrix, cmap='Reds', vmin=0, vmax=1, interpolation='nearest')

# 添加颜色条
plt.colorbar(im, label='Similarity')

# 设置坐标轴标签
users = [f'u{i}' for i in range(10)]
ax.set_xticks(range(10))
ax.set_yticks(range(10))
ax.set_xticklabels(users)
ax.set_yticklabels(users)

# 在每个格子中添加数值
for i in range(10):
    for j in range(10):
        text = ax.text(j, i, f'{sim_matrix[i, j]:.2f}',
                       ha="center", va="center",
                       color="white" if sim_matrix[i, j] < 0.6 else "black",  # 自动调整文字颜色
                       fontsize=8)

plt.title('User Similarity Matrix Heatmap with Values (First 10 Users)')
plt.tight_layout()
plt.savefig('user_similarity_heatmap_with_values.png', dpi=300)
plt.show()