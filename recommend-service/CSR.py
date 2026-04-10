import matplotlib.pyplot as plt
import numpy as np

fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(10, 4))

# 左侧：稠密矩阵
matrix = np.array([[5,0,0,1],
                   [0,0,3,0],
                   [2,0,0,0]])
ax1.matshow(matrix, cmap='Blues', vmin=0, vmax=5)
for i in range(3):
    for j in range(4):
        if matrix[i,j] != 0:
            ax1.text(j, i, str(matrix[i,j]), ha='center', va='center', color='red', fontsize=12)
        else:
            ax1.text(j, i, '0', ha='center', va='center', color='gray', fontsize=10)
ax1.set_title('稠密矩阵 (3×4)')
ax1.set_xticks(range(4))
ax1.set_yticks(range(3))

# 右侧：CSR三数组（用文本表示）
ax2.axis('off')
textstr = "data     = [5, 1, 3, 2]\n\nindices  = [0, 3, 2, 0]\n\nindptr   = [0, 2, 3, 4]"
ax2.text(0.1, 0.5, textstr, fontsize=12, verticalalignment='center')
ax2.set_title('CSR稀疏矩阵表示')

plt.tight_layout()
plt.savefig('matrix_csr.png', dpi=300)
plt.show()