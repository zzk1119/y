import cv2
import matplotlib.pyplot as plt
import numpy as np



# 直方图均衡增强 亮度

def hist(image):
    r, g, b = cv2.split(image)
    r1 = cv2.equalizeHist(r)
    g1 = cv2.equalizeHist(g)
    b1 = cv2.equalizeHist(b)
    image_equal_clo = cv2.merge([r1, g1, b1])
    imges_eq = np.hstack([image, image_equal_clo])
    # cv2.imshow('hist', imges_eq)
    # plt.hist(image.ravel(), 256)
    # plt.savefig("grayhist.png")
    #  plt.show()
    # plt.hist(image_equal_clo.ravel(), 256)
    # plt.savefig("grayhist.png")
    #  plt.show()
    return image_equal_clo


# 拉普拉斯算子  锐度

def laplacian(image):
    kernel = np.array([[0, -1, 0], [-1, 5, -1], [0, -1, 0]], np.float32)  # 定义拉普拉斯算子
    image_lap = cv2.filter2D(image, -1, kernel=kernel)  # 调用opencv图像锐化函数
    # 按要求左右显示原图与拉普拉斯处理结果
    imges1 = np.hstack([image, image_lap])
    cv2.imshow('lapres', imges1)

    return image_lap


# 伽马变换  对比度

def gamma(image):
    fgamma = 2.2
    image_gamma = np.uint8(np.power((np.array(image) / 255.0), fgamma) * 255.0)
    cv2.normalize(image_gamma, image_gamma, 0, 255, cv2.NORM_MINMAX)
    cv2.convertScaleAbs(image_gamma, image_gamma)

    return image_gamma


# replaceZeroes

def replaceZeroes(data):
    min_nonzero = min(data[np.nonzero(data)])
    data[data == 0] = min_nonzero
    return data


# -------图像质量评价
# eva 点锐度
def EVA(img):
    n = 0.6 ** 0.5  # 斜角
    s = 1.3  # 四周
    kernel = np.array([[n, s, n],
                       [s, -8, s],
                       [n, s, n]])
    dst_matrix = cv2.filter2D(img, -1, kernel)
    EVA_index = np.mean(dst_matrix)
    return EVA_index


# 均值  亮度
def Mean(img):
    (mean, stddv) = cv2.meanStdDev(img)
    return mean


# 方差  对比度
def std(img):
    (mean, stddv) = cv2.meanStdDev(img)
    return stddv


# ------------------图像类型预估算法

def predict(mean, stddv, eva): #均值、方差、点锐度
    i = 0
    j = 0
    a= [0, 0, 0]  # 按顺序分别为mean\stddv\eva
    stddv = 30 - stddv
    eva = 32 - eva
    if (mean >= 190):
        mean = mean - 190
    if (mean <= 62):
        mean = 62 - stddv
    if (mean > 0):
        a[0] = 1
    if (stddv > 0):
        a[1] = 1
    if (eva > 0):
        a[2] = 1
    if (mean >= stddv and mean >= eva):
        a[0] = 1 + a[0]
    if (mean <= stddv and stddv >= eva):
        a[1] = 1 + a[1]
    if (eva >= stddv and mean <= eva):
        a[2] = 1 + a[2]
    while (i < 3):
        # score[i] = GMSD(img0,l[i])
        if a[i] == max(a):
            j = i
        print(a[i])
        i += 1
        print(j)
    return j

# -------图像质量评价
#选出最佳图像

def origin(pic_path):  #读入图像
    image = cv2.imread(pic_path)
    return image

def img_enhance(pic_path): #选择最佳算法并输出结果图像
    image = cv2.imread(pic_path)
    image_gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    mean = Mean(image_gray);
    stddv = std(image_gray);
    eva = EVA(image);
    type = predict(mean, stddv, eva)
    if (type == 0):
        best = hist(image) #直方图均衡解决亮度问题
    if (type == 1):
        best = gamma(image)  #gamma解决对比度问题
    if (type == 2):
        best = laplacian(image) #拉普拉斯解决锐度问题
    return best


#预存图片
def ImWrite(img):
    cv2.imwrite('/storage/emulated/0/Mycamera/enhance.jpg',img)
    path = "/storage/emulated/0/Mycamera/enhance.jpg"
    return path

